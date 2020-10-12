# RobinASRHyphenationCorrection

Corrects RobinASR (https://github.com/racai-ai/RobinASR) raw output by adding hyphenation and basic capitalization based on lists of named entities.

Runs as a Java REST API service.

## Executing on Windows:
> java -cp deps\json-20200518.jar;deps\commons-lang-2.6.jar;RobinASRHyphenationCorrection.jar Server 8012 model2

## Executing on Linux:
> java -cp deps/json-20200518.jar:deps/commons-lang-2.6.jar:RobinASRHyphenationCorrection.jar Server 8012 model2

While starting it will display the following:
> Loading resources from [model2]
> All resources loaded
> Starting server on port 8012

## Testing
After starting the program you can connect with a browser by using an URL similar to:
> http://127.0.0.1:8012/correct?text=gheorghe%20%C8%99i%20ion%20sau%20dus%20la%20munte%20sau%20la%20mare
(there is a single parameter called "text" receiving the text as an urlencoded value; for larger texts this can be send via POST)

The result is a JSON similar to the following:
> {
>   "comments":[
>      "și [0] [18] [0] [0]",
>      "ion [0] [53] [0] [0]",
>      "s-au [2473] [40] [0] [0]",
>      "la [0] [2914] [0] [0]",
>      "sau [3] [119840] [0] [0]",
>      "la [0] [5136] [0] [0]",
>      "mare [0] [0] [16] [655443]"
>   ],
>   "text":"Gheorghe și Ion s-au dus la munte sau la mare",
>   "status":"OK"
> }

The two relevant fields are "status" which should be "OK" and "text" which contains the corrected text.
"comments" is an internal field describing how the model reached a decision. It's not relevant for the usage of the system.

