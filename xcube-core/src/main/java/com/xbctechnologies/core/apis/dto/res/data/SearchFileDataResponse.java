package com.xbctechnologies.core.apis.dto.res.data;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class SearchFileDataResponse extends Response<SearchFileDataResponse.Result> {
    public SearchFileDataResponse.Result getData() {
        return getResult();
    }

    @Data
    public static class Result {
        private String address;
        private long opCnt;
        private String creator;
        private String info;
        private List<String> authors;
        private String dataHash;
    }
}
