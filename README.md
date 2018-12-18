# Example google analytics

## example data

#### example_data/example_google_analytics_query.json

* query json string is base on [https://ga-dev-tools.appspot.com/query-explorer/]
    * You can supply a maximum of 10 metrics for any query. (comma delimiter)
    * You can supply a maximum of 7 dimensions in any query. (comma delimiter)
    * You can supply a maximum of 1 segment in any query. (comma delimiter)
    * Filter expressions Maximum length of 128 characters


```json
{
  "ids": "ga:{view id}",
  "start_date": "{start-date:yyyy-MM-dd}",
  "end_date": "{end-date:yyyy-MM-dd}",
  "metrics": "ga:{metricname},ga:{metricname}...",
  "dimensions": "ga:{dimension},ga:{dimension}...",
  "segment": "{segmentExpr or segmentId}"
}
```

#### example_data/example_google_credential.json

* credential json from google api service account credential

```json
{
  "type": "service_account",
  "project_id": "...",
  "private_key_id": "...",
  "private_key": "...",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "...",
  "token_uri": "...",
  "auth_provider_x509_cert_url": "...",
  "client_x509_cert_url": "..."
}
```

## License
This plugin is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).