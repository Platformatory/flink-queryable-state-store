FROM python:3.9-slim

WORKDIR /app

COPY household_power_consumption.txt household_power_consumption.txt

COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt

COPY household_power_consumption_producer.py household_power_consumption_producer.py

CMD ["python", "household_power_consumption_producer.py"]