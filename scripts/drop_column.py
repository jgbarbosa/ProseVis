#!/usr/bin/env python
import sys

def drop_col(src_path, dst_path, cols_to_drop):
    inf = open(src_path, 'r')
    outf = open(dst_path, 'w')
    for line in inf:
        fields = line.strip().split('\t')
        fixed = list()
        for idx, token in enumerate(fields):
            if (not idx in cols_to_drop):
                fixed.append(token)
        outf.write('\t'.join(fixed) + '\n')
    inf.close()
    outf.close()


if __name__ == '__main__':
    if len(sys.argv) < 4:
        print('usage: ./drop_column.py src dst column_index_from_0')
        sys.exit()
    src = sys.argv[1]
    dst = sys.argv[2]
    col = [int(sys.argv[3])]
    drop_col(src, dst, col)
