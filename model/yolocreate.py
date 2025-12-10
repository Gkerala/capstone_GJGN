import os
import shutil
import random
from tqdm import tqdm

# 클래스 이름을 OID 스타일로 변환
def normalize_class_name(name):
    return name.lower().replace(" ", "_")

# YOLO 저장 구조 생성
def make_dirs(base):
    for split in ["train", "val", "test"]:
        os.makedirs(os.path.join(base, split, "images"), exist_ok=True)
        os.makedirs(os.path.join(base, split, "labels"), exist_ok=True)

# 메인 로직
def create_yolo_dataset(src_root, out_root, classes):
    print(f"🔍 Train 폴더에서만 데이터 스캔중...")

    train_path = os.path.join(src_root, "train")
    out_base = out_root
    make_dirs(out_base)

    for cls in classes:
        cls_key = normalize_class_name(cls)
        print(f"\n📦 Processing {cls} ({cls_key}) ...")

        # train 폴더에서 해당 클래스 이미지만 수집
        images = [
            f for f in os.listdir(train_path)
            if f.lower().startswith(cls_key) and f.lower().endswith(".jpg")
        ]

        labels = [
            f.replace(".jpg", ".txt") for f in images
        ]

        total = len(images)
        print(f"  → Train에서 발견: {total}장")

        if total == 0:
            print(f"  ⚠️ {cls}: 데이터 없음 (건너뜀)")
            continue

        # 최대 300장만 사용
        selected_idx = list(range(total))
        random.shuffle(selected_idx)
        selected_idx = selected_idx[:300]

        selected_imgs = [images[i] for i in selected_idx]
        selected_lbls = [labels[i] for i in selected_idx]

        # train/val/test 비율: 240/30/30
        train_imgs = selected_imgs[:240]
        val_imgs   = selected_imgs[240:270]
        test_imgs  = selected_imgs[270:300]

        def copy_items(img_list, split):
            for img in img_list:
                label = img.replace(".jpg", ".txt")

                src_img = os.path.join(train_path, img)
                src_lbl = os.path.join(train_path, "labels", label)

                dst_img = os.path.join(out_base, split, "images", img)
                dst_lbl = os.path.join(out_base, split, "labels", label)

                if os.path.exists(src_img):
                    shutil.copy(src_img, dst_img)
                if os.path.exists(src_lbl):
                    shutil.copy(src_lbl, dst_lbl)

        copy_items(train_imgs, "train")
        copy_items(val_imgs, "val")
        copy_items(test_imgs, "test")

        print(f"  ✔ 완료: train={len(train_imgs)}, val={len(val_imgs)}, test={len(test_imgs)}")

    print("\n🎉 YOLO 데이터셋 생성 완료!")
    print(f"➡ 저장 위치: {out_base}")

# 실행 예시
if __name__ == "__main__":
    CLASSES = [
        "Pizza", "Hamburger", "Hot dog", "Sushi",
        "French fries", "Cake", "Pasta",
        "Taco", "Sandwich", "Salad"
    ]

    create_yolo_dataset(
        src_root=r"D:\capstone\diet_app\model\OIDv6\train\multidata",
        out_root=r"D:\capstone\diet_app\model\OIDv6_YOLO",
        classes=CLASSES
    )
