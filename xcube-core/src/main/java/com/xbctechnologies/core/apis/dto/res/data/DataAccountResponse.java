package com.xbctechnologies.core.apis.dto.res.data;

import com.xbctechnologies.core.apis.dto.res.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataAccountResponse extends Response<DataAccountResponse.Result> {
    public DataAccountResponse.Result getDataAccount() {
        return getResult();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String address;
        private long opCnt;
        private String creator;
        private List<String> authors;
        private String dataHash;
        private long blockNo;
        private String txHash;

        private String reserved;
        private String info;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String address;
        private long opCnt;
        private String creator;
        private List<String> authors;
        private String dataHash;
        private long blockNo;
        private String txHash;

        private String reserved;
        private String info;

        private Builder() {
        }

        public Builder withAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder withOpCnt(long opCnt) {
            this.opCnt = opCnt;
            return this;
        }

        public Builder withCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder withAuthors(List<String> authors) {
            this.authors = authors;
            return this;
        }

        public Builder withDataHash(String dataHash) {
            this.dataHash = dataHash;
            return this;
        }

        public Builder withBlockNo(long blockNo) {
            this.blockNo = blockNo;
            return this;
        }

        public Builder withTxHash(String txHash) {
            this.txHash = txHash;
            return this;
        }

        public Builder withReserved(String reserved) {
            this.reserved = reserved;
            return this;
        }

        public Builder withInfo(String info) {
            this.info = info;
            return this;
        }

        public DataAccountResponse.Result build() {
            return new DataAccountResponse.Result(address, opCnt, creator, authors, dataHash, blockNo, txHash, reserved, info);
        }
    }
}
