# AlertHub Kubernetes structure

Namespace: `alerthub`

Structure is organized as requested: one folder per microservice. Each folder has:

- `name-ms.deployment.yaml`
- `name-ms.service.yaml`
- `name-db.StatefulSet.yaml` and `name-db.service.yaml` when the service has its own DB

Shared infrastructure is under `_infra` because Kafka and Redis are not a single MS database.

## Run

```cmd
minikube start --driver=docker
cd Kubernetes
kubectl apply -f 00-namespace.yaml
kubectl apply -f 00-secrets.yaml
kubectl apply -f _infra/
kubectl apply -f user-ms/
kubectl apply -f loader-ms/
kubectl apply -f metric-ms/
kubectl apply -f action-ms/
kubectl apply -f logger-ms/
kubectl apply -f security-ms/
kubectl apply -f processor-ms/
kubectl apply -f email-ms/
kubectl apply -f sms-ms/
kubectl apply -f evaluation-ms/
kubectl apply -f gateway-ms/
```

Or on Windows:

```cmd
apply-all.bat
```

## Check

```cmd
kubectl get pods -n alerthub
kubectl get svc -n alerthub
kubectl logs deployment/user-ms -n alerthub
```

## Access Gateway

Only `gateway-ms` is `NodePort` by default.

```cmd
minikube service gateway-ms -n alerthub --url
```

Gateway NodePort: `31007`.

## Important

Before using real Email/SMS, edit `00-secrets.yaml` and replace:

- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `TWILIO_API_KEY`
- `TWILIO_API_SECRET`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_MESSAGING_SERVICE_SID`
