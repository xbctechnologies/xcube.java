package com.xbctechnologies.core.apis.dto.xtypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.xbctechnologies.core.apis.dto.ApiEnum;
import com.xbctechnologies.core.apis.dto.xtypes.proto.FileTxProto;
import com.xbctechnologies.core.apis.dto.xtypes.proto.StringProto;
import com.xbctechnologies.core.exception.TransactionException;
import com.xbctechnologies.core.utils.Base64Util;
import com.xbctechnologies.core.utils.CryptoUtil;
import com.xbctechnologies.xcrypto.util.ByteUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TxFileBody extends TxParentBody {
    private ApiEnum.OpType op;
    @JsonIgnore
    private File file;
    private String dataHash; //Must be base64 encoding
    private String reserved; //Must be base64 encoding
    private String info;
    private List<String> authors;

    public TxFileBody(ApiEnum.OpType op, File file, String reserved, String info, List<String> authors) {
        this.op = op;
        this.file = file;
        this.reserved = reserved;
        this.info = info;
        this.authors = authors;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void validate() throws TransactionException {
        switch (op) {
            case RegisterType:
                if (authors == null || authors.size() == 0) {
                    throw new TransactionException("The authors field in the file transaction must be set.");
                }
                break;
            case UpdateType:
                break;
        }
        if (file == null) {
            throw new TransactionException("The file field must be set");
        }
        if (!file.exists()) {
            throw new TransactionException("The file does not exist");
        }

        if (authors != null && authors.size() > 0) {
            boolean isAllowAll = false;
            boolean isNotAllowAll = false;

            for (String author : authors) {
                switch (author) {
                    case "0xffffffffffffffffffffffffffffffffffffffff":
                        isAllowAll = true;
                        break;
                    case "0x0000000000000000000000000000000000000000":
                        isNotAllowAll = true;
                        break;
                }
            }

            if (isAllowAll && isNotAllowAll) {
                throw new TransactionException("It is incompatible when both allow and not permit");
            }
            if ((isAllowAll || isNotAllowAll) && authors.size() > 1) {
                throw new TransactionException("If Allow and Disallow are set, no other addresses can be set.");
            }
        }
    }

    @Override
    public String getMethod() {
        return METHOD;
    }

    @Override
    public ApiEnum.PayloadType getPayloadType() {
        return ApiEnum.PayloadType.FileType;
    }

    @Override
    public Object marshalProto() {
        return marshalProto(false);
    }

    public Object marshalProto(boolean ignoreFile) {
        FileTxProto.FileTx.Builder builder = FileTxProto.FileTx.newBuilder();
        builder.setOp(op.toValue());

        if (!ignoreFile) {
            String val = Base64Util.encode(file);

            StringProto.String.Builder stringOrBuilder = StringProto.String.newBuilder()
                    .setValue(val);
            StringProto.String stringValue = stringOrBuilder.build();
            builder.setDataHash(ByteString.copyFrom(CryptoUtil.sha256(stringValue.toByteArray())));
        } else {
            builder.setDataHash(ByteString.copyFrom(Base64Util.decode(this.dataHash)));
        }

        if (reserved != null) {
            builder.setReserved(reserved);
        }
        if (info != null) {
            builder.setInfo(info);
        }
        if (authors != null) {
            authors.forEach(item -> builder.addAuthors(ByteUtil.toNoPriFixHexString(item)));
        }

        return builder.build();
    }

    public static final class Builder {
        private ApiEnum.OpType op;
        private File file;
        private String reserved;
        private String info;
        private List<String> authors;

        private Builder() {
        }

        public Builder withOp(ApiEnum.OpType op) {
            this.op = op;
            return this;
        }

        public Builder withFile(File file) {
            this.file = file;
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

        public Builder withAuthors(List<String> authors) {
            this.authors = authors;
            return this;
        }

        public Builder allowAll() {
            this.authors = Arrays.asList("0xffffffffffffffffffffffffffffffffffffffff");
            return this;
        }

        public Builder notAllowAll() {
            this.authors = Arrays.asList("0x0000000000000000000000000000000000000000");
            return this;
        }

        public TxFileBody build() {
            return new TxFileBody(op, file, reserved, info, authors);
        }
    }
}
