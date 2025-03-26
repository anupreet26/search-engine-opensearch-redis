from fastapi import FastAPI
import spacy
import re

app = FastAPI()

# Load SciSpacy Biomedical Model
nlp = spacy.load("en_ner_bc5cdr_md")

# Custom label mapping for biomedical entities
label_map = {
    "DISEASE": "DISEASE",
    "CHEMICAL": "CHEMICAL",
    "CONDITION": "CONDITION",
    "GENE": "GENE",
    "PROTEIN": "PROTEIN",
    "ENTITY": "UNKNOWN"
}

# Custom regex patterns for vital signs
vital_signs_patterns = {
    "blood pressure": r"\b(blood pressure|bp)\b",
    "heart rate": r"\b(heart rate|pulse)\b",
    "respiratory rate": r"\b(breathing rate|respiratory rate)\b",
    "oxygen saturation": r"\b(oxygen levels|oxygen saturation|spo2)\b",
    "temperature": r"\b(body temperature|temperature)\b",
    "weight": r"\b(weight|body weight)\b"
}

def extract_vital_signs(query):
    """Extract vital signs using regex patterns"""
    extracted = {}
    for vital, pattern in vital_signs_patterns.items():
        if re.search(pattern, query, re.IGNORECASE):
            extracted[vital] = "VITAL_SIGN"
    return extracted

@app.get("/normalize")
def normalize(query: str):
    # Process query with SciSpacy
    doc = nlp(query)
    entities = {
        ent.text: label_map.get(ent.label_, "OTHER")
        for ent in doc.ents
    }

    # Extract vital signs
    vitals = extract_vital_signs(query)

    # Combine results
    entities.update(vitals)
    return {"entities": entities}

# Run server using: uvicorn main:app --host 0.0.0.0 --port 8000