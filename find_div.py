html = open('temp_watch_utf8.html', encoding='utf-8').read()
idx = html.find('id="halim-ajax-list-server"')
print(html[max(0, idx-100):min(len(html), idx+500)])
