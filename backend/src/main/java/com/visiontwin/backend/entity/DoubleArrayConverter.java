package com.visiontwin.backend.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Converter
public class DoubleArrayConverter implements AttributeConverter<double[], String> {

    @Override
    public String convertToDatabaseColumn(double[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return "";
        }
        return Arrays.stream(attribute)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public double[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new double[0];
        }
        return Arrays.stream(dbData.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
