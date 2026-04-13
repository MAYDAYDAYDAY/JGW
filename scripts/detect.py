import torch
import json
import sys
from torch.autograd import Variable
from torchvision import transforms, models
from PIL import Image
from torch import nn

# 修改后的测试预处理，与训练代码一致
test_transform = transforms.Compose([
    transforms.Resize(256),
    # ====== 新增扫描件色彩处理 ======
    transforms.Lambda(lambda img: transforms.functional.autocontrast(img)),  # 自动拉满对比度
    transforms.ColorJitter(
        brightness=(0, 0.05),    # 压制过曝高光 (-5%~+5%亮度变化)
        contrast=(1.2, 1.5),        # 增强对比度 (20%~50%)
        saturation=(0.6, 0.8),      # 降低饱和度 (保留60%~80%)
        hue=(-0.05, 0.05)           # 轻微色偏修正 (±5%色调)
    ),
    transforms.Lambda(lambda img: transforms.functional.adjust_gamma(img, 0.95)),  # 加深暗部细节
    # ==============================
    transforms.ToTensor(),
])

def prepare_image(img):
    img = test_transform(img)
    img = img.unsqueeze(0)
    # 若使用 GPU，可在此处将 img 放到 GPU 上
    img = Variable(img)
    return img

# 修改 process_image：直接打开图像并应用预处理，无需复杂的透明通道处理
def process_image(image_path):
    img = Image.open(image_path).convert('RGB')
    image = prepare_image(img)
    return image

def load_model(modelPath, num_classes):
    global model
    # 使用 torchvision 内置的 mobilenet_v3_small 模型（不加载预训练权重）
    model = models.mobilenet_v3_small(num_classes=num_classes)
    # 加载训练时保存的状态字典
    state = torch.load(modelPath, map_location=torch.device('cpu'))
    model.load_state_dict(state['model_state_dict'])
    model.eval()


if __name__ == "__main__":
    path = sys.argv[1]
    modelPath = sys.argv[2]

    # 加载类别映射
    with open("C:/Users/april/Downloads/scripts/classes.json", 'r') as f:
        classes = json.load(f)

    num_classes = len(classes)
    load_model(modelPath, num_classes)

    image_data = process_image(path)
    data = {"success": False}
    with torch.no_grad():
        output = model(image_data)

    # 使用 softmax 得到预测概率，并排序结果
    softmax_output = nn.Softmax(dim=1)(output)
    confidences, labels = torch.sort(softmax_output, descending=True)
    top_confidence = confidences[0, 0].item()  # 获取最高概率值
    predicted_index = labels[0, 0].item()      # 获取预测索引

    # 根据概率阈值决定返回结果

    predicted_class = classes[predicted_index]


    data["predictions"] = [{"id": predicted_index, "name": predicted_class}]
    data["success"] = True

    print(data["predictions"][0]["name"])
