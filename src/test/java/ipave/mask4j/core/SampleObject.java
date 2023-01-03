package ipave.mask4j.core;

import java.util.List;

public class SampleObject {

    public Store store;
    public Integer expensive;

    SampleObject(Store store, Integer expensive) {
        this.store = store;
        this.expensive = expensive;
    }

    static class Store {

        public List<Book> books;
        public Bicycle bicycle;

        Store(List<Book> books, Bicycle bicycle) {
            this.books = books;
            this.bicycle = bicycle;
        }


    }

    static class Book {

        public String category;
        public String author;
        public String title;
        public String isbn;
        public Float price;

        Book(String category, String author, String title, String isbn, Float price) {
            this.category = category;
            this.author = author;
            this.title = title;
            this.isbn = isbn;
            this.price = price;
        }


    }

    static class Bicycle {

        public String color;
        public Float price;

        Bicycle(String color, Float price) {
            this.color = color;
            this.price = price;
        }

    }

}
