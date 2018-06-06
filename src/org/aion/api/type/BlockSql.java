package org.aion.api.type;

import java.util.List;

public final class BlockSql {
    private final long number;
    private final String hash;
    private final String parentHash;
    private final String block;
    private final List<String> transactions;

    private BlockSql(BlockSql.BlockSqlBuilder builder) {
        this.number = builder.number;
        this.hash = builder.hash;
        this.parentHash = builder.parentHash;
        this.block = builder.block;
        this.transactions = builder.transactions;
    }

    public long getNumber() {
        return number;
    }

    public String getHash() {
        return hash;
    }

    public String getParentHash() {
        return parentHash;
    }

    public String getBlock() { return block; }

    public List<String> getTransactions() { return transactions; }

    public static class BlockSqlBuilder {
        private long number;
        private String hash;
        private String parentHash;
        private String block;
        private List<String> transactions;

        public BlockSqlBuilder() {
        }

        public BlockSqlBuilder number(final long number) {
            this.number = number;
            return this;
        }

        public BlockSqlBuilder hash(final String hash) {
            this.hash = hash;
            return this;
        }

        public BlockSqlBuilder parentHash(final String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockSqlBuilder block(final String block) {
            this.block = block;
            return this;
        }

        public BlockSqlBuilder transactions(final List<String> transactions) {
            this.transactions = transactions;
            return this;
        }

        public BlockSql createBlockSql() {
            return new BlockSql(this);
        }
    }
}
