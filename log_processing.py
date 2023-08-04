tj_sum = 0
ts_sum = 0
num_entries = 0




f = open("/var/lib/tomcat10/logs/log")

data = f.readlines()
for line in data:
    splittin = line.split(" ")
    tj_sum += int(splittin[0])
    ts_sum += int(splittin[1])
    
    num_entries += 1

print(tj_sum/num_entries)
print(ts_sum/num_entries)
