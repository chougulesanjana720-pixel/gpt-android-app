import base64
import io
from flask import Flask, request, jsonify
from PIL import Image
from image_analyzer import ImageAnalyzer

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "service": "Python ML Image Analysis Service"})

@app.route('/analyze_image', methods=['POST'])
def analyze_image():
    try:
        prompt = request.form.get('prompt', '')
        image_bytes = None

        if 'image' in request.files:
            file = request.files['image']
            image_bytes = file.read()
        elif request.is_json:
            data = request.get_json()
            prompt = data.get('prompt', prompt)
            base64_str = data.get('image_base64', '')
            if base64_str:
                if ',' in base64_str:
                    base64_str = base64_str.split(',', 1)[1]
                image_bytes = base64.b64decode(base64_str)

        if not image_bytes:
            return jsonify({"error": "No image provided. Please send image file or base64."}), 400

        analyzer = ImageAnalyzer(image_bytes)
        raw_data = analyzer.extract_raw_data()
        response_text = analyzer.generate_response(prompt)

        return jsonify({
            "success": True,
            "prompt": prompt,
            "raw_data": raw_data,
            "analysis": response_text
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    print("Starting Python ML Image Analysis Server on http://0.0.0.0:5000 ...")
    app.run(host='0.0.0.0', port=5000, debug=False)
