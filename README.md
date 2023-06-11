# ArgoCD Demo

Project to demo ArgoCD in local machine

## Requirements

- Ubuntu
- jdk17
- docker
- minikube
- helm
- argocd cli

## Build Demo Spring Boot App

```shell
mvn clean install
```

## Minikube

Start

```shell
minikube start --driver=docker
```

Add ingress addon to minikube

```shell
minikube addons enable ingress
```

Start minikube tunnel
Leave this terminal open

```shell
minikube tunnel
```

## Hosts

You must add ingress hostnames

```shell
sudo vim /etc/hosts
```

```shell
sudo tee -a /etc/hosts << EOF
#
# argodemo local endpoints
192.168.49.2 argodemo-dev.local
192.168.49.2 argodemo-qa.local
192.168.49.2 argodemo-prod.local
192.168.49.2 argocd.local
EOF
```

```shell
cat /etc/hosts
```


## Docker

Build docker image

```shell
eval $(minikube docker-env)
docker build -t argodemo:0.0.0 .
```

## Helm

### Test run with Helm before going to ArgoCD

Inflate helm templates to k8s manifests

```shell
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/env/dev/values.yaml --output-dir k8s/output/dev
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/env/qa/values.yaml --output-dir k8s/output/qa
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/env/prod/values.yaml --output-dir k8s/output/prod
```

Apply the k8s manifests

```shell
kubectl apply -Rf k8s/output
```

Get Pods

```shell
kubectl get po
```

Delete the k8s manifests

```shell
kubectl delete -Rf k8s/output
```

## ArgoCD

<https://medium.com/@mehmetodabashi/installing-argocd-on-minikube-and-deploying-a-test-application-caa68ec55fbf>

Install

```shell
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Wait until all resources are ready

```shell
kubectl get all -n argocd
```

Get argocd secret and copy the secret output

```shell
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
```

Expose ArgoCD if you want to directly access

```shell
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Or modify ArgoCD as LoadBalancer to port 8080

```shell
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer", "ports": [{"port": 8080, "name": "argocd-server-port"}]}}'
```

Login
- login: <http://localhost:8080>
- username: admin
- password: <from-previous-secret-step>

Update password
- Click `User Info` on the left pane
- Click `UPDATE PASSWORD` on the top
- Change new password to `password`

Delete admin credential

```shell
kubectl -n argocd delete secret argocd-initial-admin-secret
```

ArgoCD CLI login

```shell
artgocd login localhost:8080
```

Push argodemo to ArgoCD

```shell
argocd app create argodemo --repo https://github.com/moredrowsy/argodemo.git  --path k8s/argodemo-chart --revision feature/ubuntu-minikube --dest-server https://kubernetes.default.svc --dest-namespace default
```