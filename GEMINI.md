# GEMINI Project Context

This document provides a comprehensive overview of the Spring AI Book project, designed to be used as instructional context for Gemini.

## Project Overview

This project is a book about building enterprise-grade RAG (Retrieval-Augmented Generation) knowledge bases using Spring AI. The book is written in Markdown, with each chapter's content in a separate `.md` file. The project also includes complete Java code examples for each chapter, located in the `code-examples` directory.

The book covers a wide range of topics, from Spring Boot basics to advanced Spring AI concepts like function calling, multimodal models, and RAG implementation.

## Directory Structure

- **Root Directory**: Contains the Markdown files for each chapter (e.g., `0.md`, `1.1.md`, etc.), along with images (`.jpg`) and project management documents.
- `code-examples/`: Contains the source code for the book, organized by chapter. Each chapter is a standalone Spring Boot project.
- `docs/`: Contains additional documentation, also organized by chapter.

## Key Files

- `CHAPTER_MAPPING.md`: The master document that outlines the structure of the book, the topics covered in each chapter, and the corresponding code examples.
- `QUICK_START.md`: A guide to quickly running and testing the projects for the first three chapters.
- `code-examples/README.md`: Provides a detailed overview of the code examples, including how to set them up and run them.
- `*.md`: The individual chapter and section files of the book.

## Building and Running the Code

The code examples are Spring Boot projects built with Maven.

### Prerequisites

- Java 21
- Maven 3.9+

### Running a Chapter's Project

Each chapter's project in the `code-examples` directory can be run using the `run.bat` script provided in its directory.

For example, to run the project for Chapter 1:

```powershell
cd E:\Spring_AI_BOOK\code-examples\chapter1-spring-boot-basics
.\run.bat
```

The API can then be tested using `curl` or a tool like Postman.

### Dependencies

The main dependencies for the projects include:

- Spring Web
- Spring Boot DevTools
- Lombok
- Validation
- Spring AI

The specific dependencies for each project are listed in its `pom.xml` file.

## Development Conventions

- The code follows standard Spring Boot project structure.
- The projects are designed to be self-contained and easy to run.
- The book's content is written in Markdown, with a clear and consistent structure.
