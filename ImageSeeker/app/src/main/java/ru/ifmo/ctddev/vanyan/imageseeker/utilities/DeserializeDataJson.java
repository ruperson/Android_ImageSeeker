package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import java.util.ArrayList;
import java.util.List;

public class DeserializeDataJson {
    public List<Photo> results = new ArrayList<>();

    static public class Photo {
        public String description;
        public Urls urls;

        static public class Urls {
            public String full;
            public String thumb;
        }
    }
}
