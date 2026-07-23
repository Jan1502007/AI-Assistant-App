import cv2
import numpy as np

# ==========================
# Camera Calibration Values
# ==========================
K = np.array([
    [440.95,   0.00, 659.82],
    [  0.00, 439.32, 451.68],
    [  0.00,   0.00,   1.00]
], dtype=np.float64)

D = np.array([
    [-0.0732],
    [ 0.1212],
    [-0.1202],
    [ 0.0384]
], dtype=np.float64)

# ==========================
# Open Camera
# ==========================
cap = cv2.VideoCapture(0)

# Set camera resolution (must match calibration resolution if possible)
cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

if not cap.isOpened():
    print("Cannot open camera")
    exit()

# Grab first frame to get dimensions
ret, frame = cap.read()

if not ret:
    print("Failed to read camera")
    cap.release()
    exit()

h, w = frame.shape[:2]
DIM = (w, h)

# ==========================
# Compute Undistortion Maps
# ==========================
balance = 0.0  # 0=crop more, 1=keep more FOV

new_K = cv2.fisheye.estimateNewCameraMatrixForUndistortRectify(
    K,
    D,
    DIM,
    np.eye(3),
    balance=balance
)

map1, map2 = cv2.fisheye.initUndistortRectifyMap(
    K,
    D,
    np.eye(3),
    new_K,
    DIM,
    cv2.CV_16SC2
)

# ==========================
# Live Camera Loop
# ==========================
while True:

    ret, frame = cap.read()

    if not ret:
        break

    corrected = cv2.remap(
        frame,
        map1,
        map2,
        interpolation=cv2.INTER_LINEAR,
        borderMode=cv2.BORDER_CONSTANT
    )

    cv2.imshow("Original", fra
    kmkme)
    cv2.imshow("Fisheye Corrected", corrected)

    key = cv2.waitKey(1) & 0xFF

    if key == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()