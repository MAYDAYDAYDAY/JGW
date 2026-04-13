import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from torchvision import datasets, transforms
from tqdm import tqdm

from MobileNetV3 import MobileNetV3_Small


def train_model(data_dir, num_epochs=20, batch_size=32, learning_rate=0.001, device='cuda'):
    # 数据预处理
    transform = transforms.Compose([
        transforms.Resize((128, 128)),  # 调整图像大小为模型输入尺寸
        transforms.RandomHorizontalFlip(),  # 数据增强
        transforms.Normalize(mean=[0.485, 0.456, 0.406],  # 常用的ImageNet归一化参数
                             std=[0.229, 0.224, 0.225]),
        transforms.ToTensor()
    ])

    # 加载数据集
    dataset = datasets.ImageFolder(root=data_dir, transform=transform)
    dataloader = DataLoader(dataset, batch_size=batch_size, shuffle=True, num_workers=0)

    # 初始化模型、损失函数和优化器
    model = MobileNetV3_Small(num_classes=1762).to(device)
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.parameters(), lr=learning_rate)

    # 训练循环
    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        running_acc = 0.0

        # 使用tqdm显示进度条
        with tqdm(total=len(dataloader), desc=f'Epoch {epoch+1}/{num_epochs}') as pbar:
            for inputs, labels in dataloader:
                inputs = inputs.to(device)
                labels = labels.to(device)

                # 前向传播
                outputs = model(inputs)
                loss = criterion(outputs, labels)

                # 反向传播和优化
                optimizer.zero_grad()
                loss.backward()
                optimizer.step()

                # 统计损失和准确率
                running_loss += loss.item() * inputs.size(0)
                _, preds = torch.max(outputs, 1)
                running_acc += torch.sum(preds == labels.data)

                # 更新进度条
                pbar.set_postfix({'Loss': f'{running_loss/(pbar.n*batch_size):.4f}',
                                  'Acc': f'{running_acc/(pbar.n*batch_size):.4f}'})
                pbar.update(1)

        epoch_loss = running_loss / len(dataset)
        epoch_acc = running_acc.float() / len(dataset)

        print(f'\nEpoch {epoch+1}/{num_epochs}')
        print(f'Train Loss: {epoch_loss:.4f} | Train Acc: {epoch_acc:.4f}')

    # 保存训练好的模型
    torch.save(model.state_dict(), 'mobilenetv3_small_trained.pth')
    print('Training complete. Model saved as mobilenetv3_small_trained.pth')

# 使用示例
if __name__ == '__main__':
    # 数据集路径，文件夹结构：data_dir/class1/xxx.jpg, data_dir/class2/xxy.jpg, ...
    data_directory = r"E:\000\39.25jiqiren\汉字转甲骨文\OBC"
    train_model(data_directory, num_epochs=40, batch_size=256, learning_rate=0.001)