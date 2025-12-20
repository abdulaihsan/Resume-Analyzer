from transformers import AutoModel, AutoTokenizer
import torch

# 1. Load the model from your local folder
model_path = r"C:\Users\Abdullah\OneDrive\Documents\SDA Project" # Path where safetensors + config.json are
model = AutoModel.from_pretrained(model_path)
tokenizer = AutoTokenizer.from_pretrained(model_path)

# 2. Create dummy input (needed to trace the graph)
dummy_input = tokenizer("Resume text here", return_tensors="pt")

# 3. Export to ONNX
torch.onnx.export(
    model, 
    (dummy_input["input_ids"], dummy_input["attention_mask"]), 
    "resume_model.onnx",  # <--- THIS is the file your Java app will use
    input_names=["input_ids", "attention_mask"], 
    output_names=["output"],
    dynamic_axes={'input_ids': {0: 'batch', 1: 'seq'}, 'attention_mask': {0: 'batch', 1: 'seq'}}
)
print("Conversion complete! Move resume_model.onnx to your Java project.")