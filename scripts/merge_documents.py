
import os

def hack():
    src1 = open('threelives.tsv', 'r')
    src2 = open('comp_threelives.tsv', 'r')
    line1 = src1.readline()
    line2 = src2.readline()
    while line1 and line2:
        line1 = line1.strip()
        line2 = line2.strip()
        part1 = line1.split('\t')[0:13]
        part2 = line2.split('\t')[10:]
        joined = part1 + part2
        print('\t'.join(joined))
        line1 = src1.readline()
        line2 = src2.readline()




if __name__ == '__main__':
    hack()
