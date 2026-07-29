import re
html = open('temp_watch_utf8.html', encoding='utf-8').read()
urls = re.findall(r'https?://[^\s\"\'\>]+', html)
matches = set(url for url in urls if 'player' in url.lower() or 'video' in url.lower() or 'embed' in url.lower() or 'ajax' in url.lower())
print("Found URLs:")
for u in matches:
    print(u)
