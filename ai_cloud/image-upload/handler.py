import boto3
import torch
import json
from torchvision import transforms

from urllib.parse import unquote_plus
import uuid
from PIL import Image
import pickle
import numpy as np
import pandas as pd
from torchvision import models
# trigger handler

# 이미지가 들어오면 feature_vector를 추출하는 역할
class Img2VecResnet18():
    def __init__(self):
        self.device = torch.device('cpu')
        self.numberFeatures = 512
        self.modelName = 'resnet-18'
        self.model, self.featureLayer = self.getFeatureLayer()
        self.model = self.model.to(self.device)
        self.model.eval()
        self.toTensor = transforms.ToTensor()
        self.normalize = transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])

    def getFeatureLayer(self):
        cnnModel = models.resnet18(pretrained=False)
        cnnModel.load_state_dict(torch.load('./model/resnet18-f37072fd.pth'))
        layer = cnnModel._modules.get('avgpool')
        self.layer_output_size = 512

        return cnnModel, layer

    def getVec(self, img):
        image = self.normalize(self.toTensor(img)).unsqueeze(0).to(self.device)
        embedding = torch.zeros(1, self.numberFeatures, 1, 1)

        def copyData(m, i, o):
            embedding.copy_(o.data)

        h = self.featureLayer.register_forward_hook(copyData)
        self.model(image)
        h.remove()

        return embedding.numpy()[0, :, 0, 0]   # 1, 512, 1, 1




def get_similarity_matrix(vectors):
    v = np.array(list(vectors.values())).T
    sim = np.inner(v.T, v.T) / ((np.linalg.norm(v, axis=0).reshape(-1, 1) * np.linalg.norm(v, axis=0).reshape(-1, 1)).T)
    keys = list(vectors.keys())
    matrix = pd.DataFrame(sim, columns = keys, index = keys)

    return matrix

def handler(event, context):
    print(event)
    print(context)
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = unquote_plus(record['s3']['object']['key'])
        tmpkey = key.replace('/', '')
        download_path = '/tmp/{}{}'.format(uuid.uuid4(), tmpkey)
        
        print(f"bucket: {bucket} | key: {key} | download_path: {download_path}")
        
        AWS_ACCESS_KEY_ID = ""
        AWS_SECRET_ACCESS_KEY = ""
        AWS_DEFAULT_REGION = ""

        s3_client = boto3.client("s3", 
                     aws_access_key_id=AWS_ACCESS_KEY_ID,
                     aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
                     region_name=AWS_DEFAULT_REGION)
        

        transformationForCNNInput = transforms.Compose([transforms.Resize((224, 224))])

        s3_client.download_file(bucket, key, download_path)
        with Image.open(download_path) as image:
            resized_image = transformationForCNNInput(image)
        
        net = Img2VecResnet18()
        vec = net.getVec(resized_image)  # 이미지로부터 벡터 추출

        s3_client.download_file('feature-only', 'img_vectors.pickle', '/tmp/img_vectors.pickle')
        s3_client.download_file('feature-only', 'similarity_matrix.pickle', '/tmp/similarity_matrix.pickle')

        try:
            with open('/tmp/img_vectors.pickle', 'rb') as v:
                all_vectors_dict = pickle.load(v)
            
            all_vectors_dict[key] = vec
            print(len(all_vectors_dict))
            
            similarity_matrix = get_similarity_matrix(all_vectors_dict)
            

            with open('/tmp/img_vectors.pickle', 'wb') as v:
                pickle.dump(all_vectors_dict, v)
            with open('/tmp/similarity_matrix.pickle', 'wb') as f:
                pickle.dump(similarity_matrix, f)
            
            print("수정된 feature files 저장 완료")
        except Exception as e:
            print("에러발생에러발생에러발생")
            print(e)

        try:
            # 수정한 파일들을 다시 업데이트 해주어야 함.
            s3_client.upload_file('/tmp/img_vectors.pickle', 'feature-only', 'img_vectors.pickle')
            s3_client.upload_file('/tmp/similarity_matrix.pickle', 'feature-only', 'similarity_matrix.pickle')

            print(f"제발 처리가 완료되었습니다. {key} 파일 uploaded!")
            print(f"현재 이미지 개수 : {len(all_vectors_dict)}")
            # s3_client.upload_file(upload_path, '{}-resized'.format(bucket), key)
        except Exception as e:
            print("파일 업로드 과정에서 error 발생")    
            print("다시 업로드하세요.")


        
        
