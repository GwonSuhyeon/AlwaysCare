

from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

import sys
sys.path.append("../../Model/yolov5/classify/")
sys.path.append("../../Model/yolov5/segment/")
import flask_server_predict
import flask_server_predict_seg

import os
import pathlib
from datetime import datetime
from collections import defaultdict


app = Flask(__name__)


# @app.route("/")
@app.route("/always-care/model/eye", methods=["POST"])
def eye_model():
    
    if request.method == "POST":
        
        save_path = "../client-request-log/"
        
        file = request.files["image_file"]
        type = request.form["image_type"]
        userId = request.form["userId"]
        
        save_path += str(userId)
        
        if pathlib.Path(save_path).exists() == False:
            os.mkdir(save_path)
        
        request_time = datetime.today().strftime("%Y%m%d%H%M%S")
        
        save_path += "/"
        save_path += request_time
        
        if pathlib.Path(save_path).exists() == False:
            os.mkdir(save_path)
        
        save_path += "/"
        save_path += secure_filename(file.filename)
        
        file.save(save_path)
        
        # print(file.filename)
        # print(info)
        
        res = prediction(type, save_path)
        
        if res == False:
            return False
        else:
            return res
    
    return False


@app.route("/always-care/model/skin", methods=["POST"])
def skin_model():
    
    if request.method == "POST":
        
        save_path = "../client-request-log/"
        
        file = request.files["image_file"]
        type = request.form["image_type"]
        userId = request.form["userId"]
        
        save_path += str(userId)
        
        if pathlib.Path(save_path).exists() == False:
            os.mkdir(save_path)
        
        request_time = datetime.today().strftime("%Y%m%d%H%M%S")
        
        save_path += "/"
        save_path += request_time
        
        if pathlib.Path(save_path).exists() == False:
            os.mkdir(save_path)
        
        save_path += "/"
        save_path += secure_filename(file.filename)
        
        file.save(save_path)
        
        # print(file.filename)
        # print(info)
        
        res = prediction(type, save_path)
        
        if res == False:
            return False
        else:
            return res
    
    return False


def prediction(type, path):
    
    eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_43/weights/best.pt"
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_44/weights/best.pt"
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_44_3/weights/last.pt" # 사용 x
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_44_5/weights/last.pt" # 사용 x
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_43_2/weights/last.pt"
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_43_3/weights/last.pt"
    # eye_model_path = "../../Model/yolov5/runs/train-cls/only_eye_43_4/weights/last.pt"
    
    # skin_model_path = "../../Model/yolov5/runs/train-seg/only_skin_1/weights/last.pt"
    # skin_model_path = "../../Model/yolov5/runs/train-seg/only_skin_5/weights/best.pt"
    skin_model_path = "../../Model/yolov5/runs/train-seg/only_skin_7_2/weights/last.pt"
    
    
    if pathlib.Path(path).exists() == False:
        return False
    
    if type == "eye":
        
        res = flask_server_predict.main(os.path.abspath(eye_model_path), os.path.abspath(path))
        
        response = {
            res[0].split(' ')[0] : res[0].split(' ')[1], 
            res[1].split(' ')[0] : res[1].split(' ')[1], 
            res[2].split(' ')[0] : res[2].split(' ')[1], 
            res[3].split(' ')[0] : res[3].split(' ')[1], 
            res[4].split(' ')[0] : res[4].split(' ')[1]
        }
        
        print(res)
        
        return jsonify(response)
    
    elif type == "skin":
        
        res = flask_server_predict_seg.main(os.path.abspath(skin_model_path), os.path.abspath(path))
        
        response = defaultdict(float)
        
        if len(res) > 0:
            
            for data in res:
                
                disease = data.split(' ')[0]
                probability = data.split(' ')[1]
                
                if float(probability) > float(response[disease]):
                    response[disease] = probability
        # else:
            
        #     response['empty'] = 1
        
        # print(response)
        print(res)
        
        return jsonify(response)
    
    else:
        return False
    
    return False


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9000)

