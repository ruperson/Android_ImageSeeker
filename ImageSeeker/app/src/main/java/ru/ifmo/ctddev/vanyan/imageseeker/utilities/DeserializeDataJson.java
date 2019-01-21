package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import java.util.ArrayList;
import java.util.List;

public class DeserializeDataJson {
    public List<Photo> results = new ArrayList<>();

    public class Photo {
        public String description;
        public Urls urls;

        public class Urls {
            public String full;
            public String thumb;
        }
    }
}
