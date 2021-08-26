import json
import pandas as pd
import boto3
import pickle
import os


def get_similar_images(similarity_matrix, img_name, num):
    k = num
    # k = similarity_matrix.shape[0]
    similar_names = pd.DataFrame(index=similarity_matrix.index, columns = range(k))

    for j in range(k):
        kSimilar = similarity_matrix.iloc[j, :].sort_values(ascending=False).head(k)

        similar_names.iloc[j, :] = list(kSimilar.index)

    print("sim_matrix 계산 성공")
    sim_names = similar_names.loc[img_name, :]

    return sim_names


def handler(event, context):

    print("event: \n",event)
    print("\n\ncontext: \n", context)
    # 메시지 내용은 request의 ['body']에 들어 있음
    try:
        request_body = json.loads(event['body'])
        print(request_body)
        img_key = request_body['content']
    except:
        print("json.loads(event['body'])호출이 불가합니다.")
        request_body = event['body']

    try:
        print(request_body)
        print("key: ",img_key)

        AWS_ACCESS_KEY_ID = ""
        AWS_SECRET_ACCESS_KEY = ""
        AWS_DEFAULT_REGION = "ap-northeast-2"

        s3_client = boto3.client(
            "s3", 
            aws_access_key_id=AWS_ACCESS_KEY_ID,
            aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
            region_name=AWS_DEFAULT_REGION
        )

        s3_client.download_file('feature-only', 'similarity_matrix.pickle', '/tmp/similarity_matrix.pickle')

        print("similarity_matrix file load : Success")
        print(os.listdir('/tmp'))

    except Exception as e:
        return {
			"statusCode": 500,
			"headers": {
				'Content-Type': 'application/json',
				'Access-Control-Allow-Origin': '*',
				"Access-Control-Allow-Credentials": True
			},
			"body": json.dumps({"error": repr(e)})
		}

    try:
        with open('/tmp/similarity_matrix.pickle', 'rb') as f:
            similarity_matrix = pickle.load(f)

        print("download된 similarity_matrix를 pickle을 사용하여 load 성공")
        
        sim_names = get_similar_images(similarity_matrix, img_key, similarity_matrix.shape[0])
        sim_dict = sim_names.to_dict()  # pandas series를 딕셔너리로 변경

        print("sim_dict 성공")
        sim_dict_to_str = str(sim_dict)
        print("sim_dict_to_str 성공")
        print("sim_dict length : " ,len(sim_dict))
    except Exception as e:
        print(e)
    
    try:
        result = "{}".format(sim_dict_to_str)
        print(result)

        return {
            'statusCode':200,
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Credentials': True
            },
            'body': json.dumps({"result": result})
        }
    except Exception as e:
        return {
			"statusCode": 500,
			"headers": {
				'Content-Type': 'application/json',
				'Access-Control-Allow-Origin': '*',
				"Access-Control-Allow-Credentials": True
			},
			"body": json.dumps({"error": repr(e)})
		}

