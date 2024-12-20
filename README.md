# QuadQueryApp_Nejah_Ouerdani by Yanis Ouerdani and Dhaker Iyad Nejah

## Objective
The goal is to develop a simple Linked Data Fragment (LDF) server capable of processing any Quad Pattern Query as Wikidata LDF. This project is implemented as an App Engine application on Google Cloud.

### Accomplished Features
- **Quad Pattern Processing**: The server processes Quad Pattern Queries similar to [Wikidata's LDF](https://query.wikidata.org/bigdata/ldf).
- **Backend**: Implemented in Java.
- **REST API**: Provides a RESTful API to:
  - Insert new quads (authentication required).
  - Retrieve quads using quad patterns (**not accomplished**).
- **Pagination**: is **not accomplished** becase the REST API has a bug.
- **Front-End**: Built with JS-Mithril.
- **Design Reference**: The application UI and API behavior mimic [Wikidata's Query Service](https://query.wikidata.org/bigdata/ldf).

### Work in Progress
- **Quad Pattern Querying via REST API**: Pending implementation.
- **Pagination Efficiency**: Requires improvements to support constant-time pagination.

