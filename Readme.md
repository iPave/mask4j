Mask4j
=====================

**A Java Library for masking JSON objects and strings.**

Library uses [JsonPath](https://github.com/json-path/JsonPath) to specify the path to be masked

Getting Started
---------------

//TODO

Mask Examples
-------------

Given the json

```javascript
{
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
            }
        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": 10
}
```


The simplest way to use is to provide a JsonPath and a Mask Function

```java
String json = "...";

JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);

String maskedJson = jsonMasker.mask(
    json,
    JsonPath.compile("$.store.book[*].category"),
    target -> target.replaceAll(".", "*")
);
```
The code above will mask all categories with function you've provided, so result will be:

```json
{
  "store": {
    "book": [
      {
        "category": "*********",
        "author": "Nigel Rees",
        "title": "Sayings of the Century",
        "price": 8.95
      },
      {
        "category": "*******",
        "author": "Evelyn Waugh",
        "title": "Sword of Honour",
        "price": 12.99
      },
      {
        "category": "*******",
        "author": "Herman Melville",
        "title": "Moby Dick",
        "isbn": "0-553-21311-3",
        "price": 8.99
      },
      {
        "category": "*******",
        "author": "J. R. R. Tolkien",
        "title": "The Lord of the Rings",
        "isbn": "0-395-19395-8",
        "price": 22.99
      }
    ],
    "bicycle": {
      "color": "red",
      "price": 19.95
    }
  },
  "expensive": 10
}
```

Masker also works with masking objects, but only with Jackson and Gson providers which can serialize objects  
```java
SampleObject sampleObject = new SampleObject(
        new SampleObject.Store(
                Arrays.asList(
                    new SampleObject.Book("reference", "Nigel Rees", "Sayings of the Century", null, 8.95F),
                    new SampleObject.Book("fiction", "J. R. R. Tolkien", "The Lord of the Rings", "0-395-19395-8", 22.99F)
                ),
                new SampleObject.Bicycle("red", 20.22F)
        ),
        10);

        String maskedJson = jsonMasker.mask(
                sampleObject,
                JsonPath.compile("$.store"),
                target -> target.replaceAll(".", "*")
        );
```
The example above will mask SampleObject and produce current masked string:

```json
{
  "store": {
    "books": [
      {
        "category": "*********",
        "author": "**********",
        "title": "**********************",
        "isbn": null,
        "price": "****"
      },
      {
        "category": "*******",
        "author": "****************",
        "title": "*********************",
        "isbn": "*************",
        "price": "*****"
      }
    ],
    "bicycle": {
      "color": "***",
      "price": "*****"
    }
  },
  "expensive": 10
}

```

When configuring masker you can specify recursion and recursion level, if recursion is set to false
and path is definite in terms of JsonWay, but found node isn't primitive there will be no masking provided.