

TakeColsAfterInc = 11
StdWIdx = 5
AddWIdx = 4

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


def hack():
    cols = list()
    cols.append(12)
    import os
    for f in ['1h4.tsv', '1h6.tsv', '2h6.tsv', '3h6.tsv', 'he8.tsv', 'bonduca.tsv']:
        src_f = 'data/shakes/' + f
        dst_f = 'data/tmp/' + f
        drop_col(src_f, dst_f, cols)
#        os.system('mv ' + dst_f + ' ' + src_f)


if __name__ == '__main__':
    hack()
