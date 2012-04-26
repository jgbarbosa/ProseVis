

TakeColsAfterInc = 11
StdWIdx = 5
AddWIdx = 4

def merge_docs(standard, additional, out_path):
    stanF = open(standard, 'r')
    addF = open(additional, 'r')
    outF = open(out_path, 'w')
    addIdx = 0
    addLine = addF.readline()
    for i, s in enumerate(stanF):
        stdTokens = s.strip().split('\t')
        if addLine:
            addTokens = addLine.strip().split('\t')
            output = stdTokens[0:12] + addTokens[len(addTokens) - 5:len(addTokens)]
            addIdx += 1
            addLine = addF.readline()
        else:
            output = stdTokens[0:12] + ['0.0' for x in addTokens[TakeColsAfterInc:]]
        outF.write('\t'.join(output) + '\n')
    outF.close()

def hack():
    import os
    for f in ['1h4.tsv', '1h6.tsv', '2h6.tsv', '3h6.tsv', 'he8.tsv']:
        merge_docs(os.sep.join(['data', 'shakespeare', '']) + f,
            os.sep.join(['data', 'comp_shakespeare', '']) + f,
            os.sep.join(['data', 'merged_shakespeare', '']) + f)

	    
def run_func():
    import sys
    if len(sys.argv) != 4:
        print 'bad num args'
        sys.exit(-1)
    merge_docs(sys.argv[1], sys.argv[2], sys.argv[3])

if __name__ == '__main__':
    hack()
