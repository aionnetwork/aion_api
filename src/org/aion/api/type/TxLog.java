package org.aion.api.type;

import java.util.List;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;

/**
 * TxLog class containing all relevant information to transaction log utilized by {@link
 * org.aion.api.ITx#getTxReceipt(org.aion.base.type.Hash256) getTxReceipt}.
 *
 * @author Jay Tseng
 * @see org.aion.api.type.TxReceipt TxReceipt
 */
public final class TxLog {

    private final AionAddress address;
    private final ByteArrayWrapper data;
    private final List<String> topics;

    public TxLog(AionAddress address, ByteArrayWrapper data, List<String> topics) {
        this.address = address;
        this.data = data;
        this.topics = topics;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int _level) {

        StringBuilder lv = new StringBuilder();
        int level = _level;
        while (level-- > 0) {
            lv.append("  ");
        }

        StringBuilder sb =
                new StringBuilder()
                        .append(lv)
                        .append("address: ")
                        .append("0x")
                        .append(address.toString())
                        .append(",\n")
                        .append(lv)
                        .append("data: ")
                        .append("0x")
                        .append(data.toString())
                        .append(",\n")
                        .append(lv)
                        .append("topics: ")
                        .append("\n")
                        .append(lv)
                        .append("[")
                        .append("\n");

        int cnt = topics.size();
        for (String s : topics) {
            sb.append(lv).append("  0x").append(s);
            if (--cnt > 0) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append(lv).append("]").append("\n");

        return sb.toString();
    }

    public AionAddress getAddress() {
        return address;
    }

    public ByteArrayWrapper getData() {
        return data;
    }

    public List<String> getTopics() {
        return topics;
    }
}
