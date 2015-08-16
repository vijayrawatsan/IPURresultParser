package com.vijayrawatsan.ipresultparser;

/**
 * Created by vijay.rawat01 on 8/1/15.
 */
public class Triplet {
    public String first;
    public String second;
    public String third;

    public Triplet(String first, String second, String third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "Triplet{" +
                "first='" + first + '\'' +
                ", second='" + second + '\'' +
                ", third='" + third + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triplet triplet = (Triplet) o;

        if (!first.equals(triplet.first)) return false;
        if (!second.equals(triplet.second)) return false;
        return third.equals(triplet.third);

    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        result = 31 * result + third.hashCode();
        return result;
    }

    public String concatenated() {
        return third.replaceAll(" ", "_") + "_" + first.replaceAll(" ", "_") + "_" + second.replaceAll(" ", "_");
    }
}
