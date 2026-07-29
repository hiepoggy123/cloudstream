import requests

post_id = "16613"
ep_key = "tap-8"
mainUrl = "https://hhpanda.st"

sv_options = ["1", "2", "3", "Vietsub", "Thuyết Minh", "vip"]
type_options = ["1", "2", "3", "4K V1", "4K V2", "4K V3", "Vietsub", "VIP", "vip", "VIP 1", "VIP 2"]

for sv in sv_options:
    for type_val in type_options:
        url = f"{mainUrl}/player/player.php?action=dox_ajax_player&post_id={post_id}&chapter_st={ep_key}&type={type_val}&sv={sv}"
        res = requests.get(url)
        if "not-found.jpg" not in res.text:
            print(f"FOUND! sv={sv}, type={type_val} -> {res.text}")
print("Done testing.")
