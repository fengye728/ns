import sys

def extractSymbolByOption(filename):
    symbolDict = {}
    with open(filename) as f:
        lines = f.readlines()
        for line in lines:
            symbol = line.split(',')[1]
            symbolDict[symbol] = 1

    with open('./symbol', 'w') as f:
        for symbol in symbolDict.keys():
            f.write(symbol + '\n')


extractSymbolByOption(sys.argv[1])