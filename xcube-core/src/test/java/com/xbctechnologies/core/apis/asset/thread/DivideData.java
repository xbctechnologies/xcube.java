package com.xbctechnologies.core.apis.asset.thread;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class DivideData {
    public static List<List<String>> divideListString(List<String> data, int divisionCnt) {
        List<List<String>> list = new ArrayList<>();

        if (data.size() <= divisionCnt) {
            list.add(data);
            return list;
        }

        int quotient = data.size() / divisionCnt;
        int remainder = data.size() % divisionCnt;

        int startIdx = 0;
        int endIdx = 0;
        for (int i = 1; i <= quotient; i++) {
            endIdx = (divisionCnt * i);
            list.add(data.subList(startIdx, endIdx));
            startIdx = endIdx;
        }

        if (remainder > 0) {
            list.add(data.subList(startIdx, data.size() - 1));
        }

        return list;
    }

    @Data
    @AllArgsConstructor
    public static class LongData {
        public long start;
        public long end;
    }

    public static List<LongData> divideListLong(long maxCnt, int divisionCnt) {
        List<LongData> list = new ArrayList<>();

        if (maxCnt <= divisionCnt) {
            list.add(new LongData(1, maxCnt));
            return list;
        }

        int quotient = (int) maxCnt / divisionCnt;
        int remainder = (int) maxCnt % divisionCnt;

        int startIdx = 1;
        int endIdx = 0;
        for (int i = 1; i <= quotient; i++) {
            endIdx = (divisionCnt * i) - 1;
            list.add(new LongData(startIdx, endIdx));
            startIdx = endIdx + 1;
        }

        if (remainder > 0) {
            list.add(new LongData(startIdx, maxCnt));
        }

        return list;
    }
}
