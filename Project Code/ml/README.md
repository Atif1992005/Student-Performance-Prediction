# Student Academic Risk Prediction — Python ML

```bash
cd ml
pip install -r requirements.txt
```

## Stage 1 — data inspection and EDA

```bash
python run_stage1.py
```

## Stage 2 — model training

Stratified 80/20 split; Logistic Regression, Decision Tree, Random Forest.

```bash
python run_stage2.py
```

## Stage 3 — FastAPI serving

Primary model: Logistic Regression (best Stage 2 macro F1).  
Comparison model: Random Forest.

```bash
python run_stage3.py
python run_api.py
```

`POST http://127.0.0.1:8000/predict` with the 10 features as JSON.  
Swagger UI: http://127.0.0.1:8000/docs
