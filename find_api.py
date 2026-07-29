import re
js = open('streamfree_app.js', encoding='utf-16').read()
for match in re.findall(r'"(/[a-zA-Z0-9_\-\.]+)"', js):
    print(match)
