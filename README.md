# ArgoCD Demo

Project to demo ArgoCD in local machine

## Requirements

- Ubuntu
- jdk17
- docker
- minikube
- helm
- argocd cli

## Helm

### Test run with Helm before going to ArgoCD

Inflate helm templates to k8s manifests

```shell
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/na/dev/values.yaml --output-dir k8s/output/na/dev
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/na/qa/values.yaml --output-dir k8s/output/na/qa
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/na/prod/values.yaml --output-dir k8s/output/na/prod
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/sa/dev/values.yaml --output-dir k8s/output/sa/dev
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/sa/qa/values.yaml --output-dir k8s/output/sa/qa
helm template k8s/argodemo-chart/ --values k8s/argodemo-chart/envs/sa/prod/values.yaml --output-dir k8s/output/sa/prod
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
kubectl port-forward svc/argocd-server -n argocd 9080:443
```

Or modify ArgoCD as LoadBalancer to port 9080

```shell
kubectl patch svc argocd-server -n argocd --type merge -p '{"spec": { "type": "LoadBalancer", "ports": [ { "name": "http", "port": 9080, "targetPort": 8080, "protocol": "TCP" }, { "name": "https", "port": 9443, "targetPort": 8080, "protocol": "TCP" } ] } }'
```

Login

- login: <http://localhost:9080>
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
argocd login localhost:9080
```

Two ArgoCD deployment options:

- Create Application:

  ```shell
  argocd app create argodemo --repo https://github.com/moredrowsy/argodemo.git  --path k8s/argodemo-chart --revision feature/ubuntu-minikube --dest-server https://kubernetes.default.svc --dest-namespace default
  ```

- Create AppplicationSet:

  ```shell
  kubectl apply -f k8s/argocd/application-set.yaml
  ```

  ```shell
  kubectl delete -f k8s/argocd/application-set.yaml
  ```
