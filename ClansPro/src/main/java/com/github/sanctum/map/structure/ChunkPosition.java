package com.github.sanctum.map.structure;

import java.util.Objects;

public class ChunkPosition {
    public final int x;
    public final int z;

    public ChunkPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkPosition(int[] arr) {
        if (arr.length != 2) throw new IllegalArgumentException("Invalid array format!");
        this.x = arr[0];
        this.z = arr[1];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkPosition) {
            final ChunkPosition chunkPosition = (ChunkPosition) obj;
            return (chunkPosition.x == x) && (chunkPosition.z == z);
        } else {
            if (obj instanceof int[]) {
                final int[] int_arr = (int[]) obj;
                if (int_arr.length == 2) {
                    return (int_arr[0] == x) && (int_arr[1] == z);
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
