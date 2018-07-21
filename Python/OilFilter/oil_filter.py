import sys


OIL_FILENAME = './oilcompanylist.csv'

oil_symbol_list = []
with open(OIL_FILENAME, 'r') as file:
    lines = file.readlines()
    lines.pop(0)
    for line in lines:
        oil_symbol_list.append(line.split(',')[0].strip())

result = []
with open(sys.argv[1], 'r') as file:
    lines = file.readlines()
    result.append(lines[0])
    for index in range(1, len(lines)):
        symbol1 = lines[index].split(',')[0].strip()
        symbol2 = lines[index].split(',')[1].strip()
        if symbol1 in oil_symbol_list or symbol2 in oil_symbol_list:
            result.append(lines[index])


out_filename = sys.argv[1][: sys.argv[1].rfind('.')] + '_oil.csv'
with open(out_filename, 'w') as out:
    for line in result:
        out.write(line)

    print('Result file:', out_filename)


