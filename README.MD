#Demo of Ansible playbook and Jenkins pipeline

##Ansible

###Configuration

####Cache
We use REDIS cache to persist Ansible fact.
Below an example of configuration:

```python
[defaults]

fact_caching = redis
fact_caching_prefix=demo_
fact_caching_connection=localhost:6379:0
```