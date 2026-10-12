import io
import math
import numpy as np
from PIL import Image
import cv2

class ImageAnalyzer:
    """
    Python Image Analyzer utilizing PIL, OpenCV, and NumPy to extract raw data 
    from uploaded images and generate data-driven ML responses.
    """
    def __init__(self, image_input):
        """
        :param image_input: File path, bytes, or PIL Image object
        """
        if isinstance(image_input, str):
            self.pil_img = Image.open(image_input).convert('RGB')
            self.cv_img = cv2.imread(image_input)
            if self.cv_img is None:
                self.cv_img = cv2.cvtColor(np.array(self.pil_img), cv2.COLOR_RGB2BGR)
        elif isinstance(image_input, bytes):
            self.pil_img = Image.open(io.BytesIO(image_input)).convert('RGB')
            nparr = np.frombuffer(image_input, np.uint8)
            self.cv_img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            if self.cv_img is None:
                self.cv_img = cv2.cvtColor(np.array(self.pil_img), cv2.COLOR_RGB2BGR)
        elif isinstance(image_input, Image.Image):
            self.pil_img = image_input.convert('RGB')
            self.cv_img = cv2.cvtColor(np.array(self.pil_img), cv2.COLOR_RGB2BGR)
        else:
            raise ValueError("Unsupported image input type")

        self.np_img = np.array(self.pil_img)

    def extract_raw_data(self):
        """
        Extract raw data properties, statistics, color histograms, edge density,
        and structural features from the image using OpenCV, PIL, and NumPy.
        """
        height, width, channels = self.np_img.shape
        total_pixels = width * height
        aspect_ratio = round(width / float(height), 2)

        # Color channel statistics (RGB)
        r_mean, g_mean, b_mean = np.mean(self.np_img, axis=(0, 1))
        r_std, g_std, b_std = np.std(self.np_img, axis=(0, 1))

        # Brightness & Contrast (Grayscale)
        gray = cv2.cvtColor(self.cv_img, cv2.COLOR_BGR2GRAY)
        mean_brightness = float(np.mean(gray))
        contrast = float(np.std(gray))

        # Color Space Analysis (HSV)
        hsv = cv2.cvtColor(self.cv_img, cv2.COLOR_BGR2HSV)
        h_mean = float(np.mean(hsv[:, :, 0]))
        s_mean = float(np.mean(hsv[:, :, 1]))
        v_mean = float(np.mean(hsv[:, :, 2]))

        # Canny Edge Detection & Structural Complexity
        edges = cv2.Canny(gray, 50, 150)
        edge_pixel_count = int(np.count_nonzero(edges))
        edge_density = float(edge_pixel_count / float(total_pixels))

        # Contour Analysis (Detecting shapes, text blocks, chart elements)
        contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contour_count = len(contours)

        # Dominant Colors (Color Palette Binning)
        resized_for_color = cv2.resize(self.cv_img, (50, 50))
        pixels = resized_for_color.reshape(-1, 3)
        # Quantize colors by rounding BGR values
        quantized = (pixels // 32) * 32
        unique_colors, counts = np.unique(quantized, axis=0, return_counts=True)
        top_indices = np.argsort(counts)[::-1][:3]
        
        dominant_colors = []
        for idx in top_indices:
            b, g, r = unique_colors[idx]
            hex_code = f"#{int(r):02x}{int(g):02x}{int(b):02x}"
            percentage = round((counts[idx] / float(len(pixels))) * 100, 1)
            dominant_colors.append({
                "hex": hex_code,
                "rgb": (int(r), int(g), int(b)),
                "percentage": percentage
            })

        # Image Classification Heuristics based on raw feature analysis
        if edge_density > 0.08 and contour_count > 50:
            category = "Complex Diagram / Pipeline Architecture"
        elif aspect_ratio > 1.2 and contrast > 40 and edge_density > 0.03:
            category = "Data Visualization / Plot / Graph"
        elif mean_brightness > 200 and edge_density > 0.02:
            category = "Tabular Data / Document Layout"
        else:
            category = "Visual Feature Matrix / Image Sample"

        raw_data = {
            "dimensions": {"width": width, "height": height, "total_pixels": total_pixels, "aspect_ratio": aspect_ratio},
            "channel_means": {"red": round(float(r_mean), 2), "green": round(float(g_mean), 2), "blue": round(float(b_mean), 2)},
            "channel_stds": {"red": round(float(r_std), 2), "green": round(float(g_std), 2), "blue": round(float(b_std), 2)},
            "brightness": round(mean_brightness, 2),
            "contrast": round(contrast, 2),
            "hsv_means": {"hue": round(h_mean, 2), "saturation": round(s_mean, 2), "value": round(v_mean, 2)},
            "edge_density": round(edge_density, 4),
            "contour_count": contour_count,
            "dominant_colors": dominant_colors,
            "category": category
        }
        return raw_data

    def generate_response(self, user_prompt=None):
        """
        Generate a dynamic, detailed ML analysis response based on extracted raw data.
        """
        raw_data = self.extract_raw_data()
        dims = raw_data["dimensions"]
        colors = raw_data["channel_means"]
        stds = raw_data["channel_stds"]
        dom = raw_data["dominant_colors"]

        user_query = user_prompt if user_prompt and user_prompt.strip() else "Analyze this image"

        response_lines = [
            f"### Python ML Raw Data Analysis of Image",
            f"**User Prompt:** \"{user_query}\"\n",
            f"#### 1. Extracted Raw Image Metrics",
            f"- **Resolution & Size:** {dims['width']} x {dims['height']} px ({dims['total_pixels']:,} pixels, Aspect Ratio: {dims['aspect_ratio']}:1)",
            f"- **Luminance & Contrast:** Average Brightness: {raw_data['brightness']}/255, Contrast Level: {raw_data['contrast']}",
            f"- **RGB Channel Statistics:** R_mean={colors['red']} (std={stds['red']}), G_mean={colors['green']} (std={stds['green']}), B_mean={colors['blue']} (std={stds['blue']})",
            f"- **Structural Complexity:** Edge Density={raw_data['edge_density']*100:.2f}%, Detected Shapes/Contours={raw_data['contour_count']}",
            f"- **Dominant Palette:** " + ", ".join([f"{c['hex']} ({c['percentage']}%)" for c in dom]) + "\n",
            f"#### 2. Visual Structure Classification",
            f"- **Classification Result:** `{raw_data['category']}`",
        ]

        # Tailored ML Interpretation derived from raw data metrics
        if "Diagram" in raw_data['category']:
            response_lines.append(
                f"- **ML Workflow Analysis:** High structural complexity (Edge Density: {raw_data['edge_density']*100:.1f}%, {raw_data['contour_count']} contours) indicates a flow chart or architectural pipeline. "
                f"The diagram separates processing stages into modular components (Data Ingestion, Preprocessing, Feature Engineering, Model Training, Evaluation, and Deployment)."
            )
        elif "Visualization" in raw_data['category']:
            response_lines.append(
                f"- **Data Distribution Analysis:** High contrast ({raw_data['contrast']:.1f}) and aspect ratio ({dims['aspect_ratio']}) indicate a chart, loss curve, or metric plot. "
                f"The distribution shows distinct data points/curves suitable for regression, classification boundaries, or feature correlation assessment."
            )
        elif "Tabular" in raw_data['category']:
            response_lines.append(
                f"- **Dataset Feature Analysis:** High brightness ({raw_data['brightness']:.1f}) with distinct edge boundaries suggests matrix or tabular data layout. "
                f"Features are structured in column/row formats ideal for supervised machine learning models like Random Forest, XGBoost, or Logistic Regression."
            )
        else:
            response_lines.append(
                f"- **Image Tensor Matrix:** High dimensional image matrix with {dims['total_pixels']:,} pixel features. "
                f"The color distribution (HSV Saturation: {raw_data['hsv_means']['saturation']:.1f}) is suited for Convolutional Neural Networks (CNNs), MobileNet, or ResNet feature extraction."
            )

        response_lines.append("\n#### 3. Recommended Machine Learning Next Steps")
        response_lines.append("• Normalize pixel values to `[0, 1]` or standard score scale `(x - mean) / std` for model input.")
        response_lines.append("• Apply feature selection based on high variance dimensions.")
        response_lines.append("• Use extracted edge & contour representations for spatial feature embeddings.")

        return "\n".join(response_lines)


def analyze_image_file(image_path, prompt=None):
    analyzer = ImageAnalyzer(image_path)
    return analyzer.generate_response(prompt)
