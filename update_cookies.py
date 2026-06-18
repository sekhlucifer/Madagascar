import json
import os
import re

cookie_text = """__cf_bm	FyFK82uKhj7zOqzqlwRtTR.GqNzHopVmNld2Zh48OGU-1781671246.8746984-1.0.1.1-XBe3CW09j_Vj6Z9uXVg0Xx4CvUNaanCu8T3N60i1cyh3G6cfASa7KEHYJge44QLCB6pxs5oG8Wl3DUAEDXwGLb7R9upuzAnssYzRyQKQxw8JIrur9.G9_HGBJ9q5SQ3H	.theoncologyinstitute.com	/	2026-06-17T05:10:46.745Z	206	✓	✓	None			Medium
__RequestVerificationToken	VXCzRjxJwfeFga83gUK2lEQOnJrJj0276wmZxL1OjkEu5S8BHkDaeeNaVYVsONgseFCDISy53kfcIauFRRYy_VN7SCJ7Lr0_MNqtKaQqrzo1	utilization-management.powerappsportals.com	/	Session	134	✓	✓	Lax			Medium
.AspNet.ApplicationCookie	chunks:2	utilization-management.powerappsportals.com	/	Session	33	✓	✓	Lax			Medium
.AspNet.ApplicationCookieC1	_-YaqtEkVjIs2HLxCVHmc1kv7sZi7Rf_F2xYUYOtop29qkpJVMP2fyyDPY54i2KRi6rtq0upkYWvQYVVbbp7d2pvrHWYhQ3ayrfIM50fyO_Nv3h_tAksEDU5xKlPsUHu-3NQ-UgHd6zmBd4BluU-q2ZyRnOA-pQdONbCxZcK0UFOeYbp89Dsv1MVV-GFCC0AVbDFz0F5MHVDxKJNm_BrvVhLc2YNomxsQz2cCavCUENUzHpx2ZS4WNorL9LBF6bWQc_egjLXpeL1U7ccXtSUlwJ7_5Y7cbc0DTfMw4WCkDi825_xV43do7npc6k6Fu3AGqyO_VqYjsTUv2BUI1DvwjeVQlBAq5wLVWdFBMN1469GTe9vp1C1kvC4Kcbcz2he9NsnwOriP2NfTiAddMSJkyHTBkcPs2EgOc22iQqROaPldAOEEf7j9qbwfnh0yaqEKCm2nEUff7szmX9ETRIJ7lJj76I1XUM_o_ODCR3o6aMMeheqqHCV1C9Vg6ZoNrI52zEjC12SAnYDw1q8TMpdOoEGb8BYX5sBqOHZqAV27J0vZ1BrDAS-vcpYv4lcILiuayNl39aOuFbN_Agwn5DYwWCQVDP_Y5ce6OiVJInRK-C-ocUm9vfKXVbdBkcOF-ikBRLGl72VywVlDaAY9yfw3OjshGv5Vdo30oMP0X868LFN4WrrjvOnsnvB3jZf33H6mjhlV-pLurdAbu36ZTm-8TZ8d6x-09nIpQ0fQlnApK_SUjlQLLxXCvyF_7HMxpJBhGOQKx7gQvdwTwHnxZ3c6Det1MC4bCp7BU3ARqicO1tWop5RkznLinI8dQPts9MUwsYAHrWY6UcUpdA3Dh82T_HfEMOIub56QmRxgjt0UzLPp_ZqvSST4tSd0QEpuhp8Y7lEelmoHd0OS2f63T8C6ocULMqxfSNMAzJIK1drmazKJFslyxzImpeT56zGBZ424H8QJHHdjZFFaeX38YU1jSMPcgVlJO78FQHcZuN4o6RziPcCE4HVFAkCNahtzzgnVU5JbLHBr9n8_84rz24uzx_ZdxR0qWVy59ZcC3JelNfqlSWpkFH5zyRGINs1bNQSE_D-7kd4xGMegRG_Rr6tZkI6_AoJC1ntjeqUhlHiegsrMGctlzZACILfFnM-oFcJOHcc0_RqpatMR-QKBPtXDMkoNo_O5nB4272oz6dUENwUcD1xnF2FKO4lpiZnkYkvoztE9wXb8OojUJqXu72HPichZg4j5Bv_6zml-zdn1kYJ7pBPVVE-h3KSHIIAPzUoFC73-gn0Tn1GtJqcQ-pe6Pg-SIoai27BoGRexO3-pkENRKtWjoCQPONg60ECsG7sYOGdOdDqjP8aV1vZ0SgY91-HeYAmwG2hak9k7N1LwLgHs7nsj6MFmH_RLjt2M91GYmxrzx3ntG7CjTCFgm2o3R12su1bVSWxa7DzA5h7zO7ncV9-EF1LU2Z_hccFJe_9wEdrkyzJcnHLx4cTz5D1Ih_rfeFlUZbVxA6SZM4lZujKWePMAvg7QV1EHc3MdO-mJJOJeGPlqQxr9EAo-35fGy0CVCoOQRxJsejb2I0SFySsU0UbT8Ye0uDoJW4BOFOQxS9ikA4qxCzvjDQWGRg-mInjuLEq81s3LRDcx0qTqaWmSP8enHSMAIj8RrYpl-Dr0VM0uxf9Dk08NEZrWCf0HG6fMu8gwdoXOIORPjWN1V22U09d_-y692gTlxWki__8RxlLpOs7H8dTs0uWJXMLlPFEGknjU2TKMFps8sH-l2fs-oSWMJwdvlptqF-xc5p5NkaVvmMnErY4ev5dNr51cbpa4DEI8SEnASxGVExRq8ghOvzRoH6x0LG8EoElL-EDj3hsTRuCEc3hj7e61BI3EMMnyH3EvQFK9hfeXIujUWJTY_xeKK1gwwZEWUFTnJ4MtN5Huguk9M6EC_jBfFuzw2tvi1pkpgD3bmh1nSnQBxlt5O5TObYcIEp038Mv7cSNFWM_4iE5NIAeHF3I9K_nZjmv2NabkQMYQKIue-kuUc33f30af-cpiORUp0CCB6zPXTKM4WZKOaJ-hMVwRXTMc7_11vuAK5S0zgIzvfJC41Kli5SZlk7t6L73rF_C_qpKCg-O2GjEINIX0K44cDFBM553mMagdJ9Daco1n5sd3uFwxt-ih--tUjtuRWvYQV5meTqftP9g8VRtGokn8wkGLSgZwHQGw89ar40nlL8ynDOBphkvvTr0mcnc0ffa6LmEefMGwG9Q_5z5vSwgJOiLgqMZUUb5PrLoxuHUIRZj2jyUgnDjQ2pLSjziPCecvRp0tVOVUPVnIIwTzH4GsHQ-xTXv2XqBVFTT2MAy31eSrZUAqzryk4-sfl4wo-21JufhgplCvOcCzgYXwdlgLh_lwVX-MoU7-Y5EgzVXUbsQUmOPKh-7YbBj8lnA8e5bY8hFHFZ2vYX4PEl6atUPx3u4j6RWkoQ2QKghII-xzKHCvS4ydnqSsy1IFXnW_o5_3zjAdS8otT7dyjs3U9OT6wN0iqBwYydGbrr4_uJBr9raTBIQEjjBokXF8M0nRjlHNIHKEoXZ3NKIOtJEP8LGHhKdK8jsVRqiqrfbHgAjKFLbzqL7JvXB1X8700tzALd2JTzYgWjkGhMgIuEKXQ7A3mWeqOW0YkmG32JjK9-9pLLUghFsCOLE3XeckQuf-NSjiJzwfEbUdNdu7uPbjc2m7YFmWEuqPIbTW3aEdBiAc_J2gHF-JN0gyT3daWCDcyoF1kdMa9dBsYL3NOZXkfdL7bKaRu4jEiCboRb5RxeafIJZs9RH1_4mBtH9bmdWA21T4ldcSX5w1lFaW49sEzQgj5nXmM3QxGc7A_4QrffTm7A-K9BkDuiYcCNWCYUq4IIKw2TDSgLjwS9l1kw4uEa86w9WCg_lsnUfi6CwKigIfolIjx45JCvyppCPISbYv0653Yx42Rpph2_gJ6S7EqNOl1Z1cCXnf9Prle3l_-tMDVEkmd1-B2jzOv0cEq54Y8NvXrp2gnMqMJx-Zu1C8uBcmB3TL8J6qgbDYO-NBjjMpNblOeRTYJB2UmAcUcHbAOVaDmuZkkr3DYTohcXTRj5d_X7ScVkzGymLBjfM8W8Jk3NUYVC1TvJOZBkn__vtdwDl__BMZ25p4YVwzM8-R9VjRxXi_sXRqa7T6z8dgUzD9wo3mZKLIdm3YZcGkGAAjpU5s5MUe2ONmPBnSrBSY_mkbxcX4_jqdj4Ku_dLM4JYdrJP9kRt437ujq49XsJXwJ64arUjEqCp4ntUnLnH9PXJoHURVwWi3bc4QOcRxkcrw3oihVinGI4XEf4KF6g-ErtZNUMF1m-kxlnV3OXrllSLxQnfJqRfrui3aiWxfF0PeG1dBTVEBfUJUjzgIMe1rFPsWmy_v8i_xdeSGoIdEDHhy-EOXCk8GHZwt-xCXhyJUD_o1no4f-PZ00jp7FbnN43_GodUociBG7-7K1munizhIeSYtPCri9E3elKQ02CD5jhObXZ_TBka25kC3KHWaFiSfXDPg-91mRwHNIb93USWXLgML7W5rD5ngAbDuAAqzfCZ1j-VlVn8AV-8LdEovHD7NuRR2_x0R5EoRlrY3bCBx6iubyIc9OdStmOjhHwJoEOWtA2j1WbMXEiJEHXb1WtduedfJPF_8QmLeUJi14vgfjLjPXUDud8PjgM9-a3PMqq9XMSMdrBEwnvS6ysYmulCf1tsOhl-VlN6VAgDE5OM5qd3B37ZEHeI278OoGuJ1hyNOdQXh5xK-ANwlpO5ns0DrFQth1H-hP8T0UR7IeZl3gMU7uLHHSTY3R9F1lGAs1vj7wSozP01Kw0ZZwRLCLomWu9XwHT9gh6KVR5wJ5KrjdGn5czAtOZP6owDn43kPriqqpzBX21g2JzGw9_xoDJUTwHBczOsCIvyE3f62UC8S4PANJE4qSXN5zb4D9Vq_ZMyEA8o1RaxuKrMFMAvR0kxolqDnMvAVW6zdJywATyhKfHjGNt2zk9g6j4cszbKIRaM-C3k2k-k7Wj_9	utilization-management.powerappsportals.com	/	Session	4048	✓	✓	Lax			Medium
.AspNet.ApplicationCookieC2	g8UzEv9Jx2H-bqxAMOV4XFUzVJmSY_w0V6CwTzqPYZQ55_iYzIBMDpwqFN91pXkH-frEUGX8yRO6yxIUZ9XJDvahTx9WX0zegPr8djFX2ZlZbQdv12T9JXWdH6Az2ix9rXnR1_M5KPBLTzyxUgCbGq25ZetllgROkoloY1aSsNx9K-r1Z0dg-iNoPzrS35vcoRq_m_nki0nvRgPDSDQpwTeIrXzHc08CKap3snTfImUocpwfgVKjOxFBQWVdBkCVu6riQ_H_m03zczf18VyX9hXM64Bm3aRQL6XhRQuA4K_j3sylCoqY7qjIRoV5fNSNQfDHYLPv_3qo5Rc3tQwjqwx_ke6bv32K8ASYUlueQz4swgkCz_G0kIMPSbjS2C2BQwJZnfvBQBB-h-Dfz0EQnQZ1AP5lTLwJfqKqayBZUCqkPkSRxIUdxL6YA2d6qMSNrKt-jxQwEXHO-clCgG3GvRSPasgAM3gY5-TdS4Y2AkbgdYdh3F5mzc5ee5jsKoWIihWlhotIaXIkmfrHTlnR4wd8p7ISk2P6HqFa3t39afxFWfrDjMysiiKX7ldeHkfxRPq0P_FowAYUhxBt-wADJSICEX6n1gmlgvsrQIiMOuA2YZfg3Nmaf4vcp7kcUWG8-Aaz_FXP_hLb-rtBgmiSW7j7Msj4wjVGArCsmEItsGR7oylTACaq5rRcw3wQip_WgrCkIO90A2YAfdohWzb8NiruQg_juwPZcxLqhyYUdaxy9cL4tm8p-sdU8nxcP3GLuwqJkJ--VGqODIwhLs4S0KnHfg17WMyesT3Vzlndmxtg-L0F1RXvLs5pftXSF-uVh3wiEUeYG-PCsDumlwiCG0Bx1mbLEADxLUoWi6i1Y0yLSBJUXgcNwl0AFV8pDp2lPQxr4rz73jIUrQiCv-v9q1i1DKmYtr8zW2wDX6k9iGKriAJeTLoPerEfXSQqGD_y6-Oa9l1Whrr0so28H-p-7GcGDUEe3UM9jhGB28qu4lH4Y1n3kIL6BLshtG35q1tQIGTnq2Zouc2mM2dU9g5nXT-blI2_3Uh7SCRgfh1-rLTJm-_qmISyM6pAyH8ZOoHJ3QjJwt4Vsw-xv_XidoPl7xHVhzhlQkE8ZfAmHAxe_CVXHGtr2J4-2WtxqTyaD-Hg35NTG5pLEVCz1Hux2wE_wa1NczoYLJCqQVbtTC-i9nOIp7uxO6ii127q3d0-Q6GXdS1bzS7s4kQm4aqUeV_WMMmIrH8TXpUWjwFVMCeaIzZwVXHJu7VnMB-4EAbTXdNwZmvvnufuBalg6YgAwSoaunOejJABPjaHVGLhOivKwF3de7w1Ba9tGc3ZzYOYfy8yqyZnPYg2EF6xHMaxIwNt1WFLhHLj8eFzza8f9NJEiQz69yUu_tXTrGdo32iev3q_pGJ5vJiWVYaD086Zd7_xSiUZNiTRAq7oA	utilization-management.powerappsportals.com	/	Session	1468	✓	✓	Lax			Medium
ARRAffinity	4523111ef8d59670c2b3d905e6462244f212b1d6628d842487d1d475ca2ce1d4	.utilization-management.powerappsportals.com	/	Session	75	✓	✓				Medium
ARRAffinitySameSite	4523111ef8d59670c2b3d905e6462244f212b1d6628d842487d1d475ca2ce1d4	.utilization-management.powerappsportals.com	/	Session	83	✓	✓	None			Medium
ContextLanguageCode	en-US	utilization-management.powerappsportals.com	/	Session	24		✓	Lax			Medium
Dynamics365PortalAnalytics	BxhVcIt_jV0TYVU2u9z18qtAUIOFjk3tpP6U6dWdy7rZw1HjmUGkUn9_pZiW2XFCguhjQWFR4IKyfWQDnNpwJBiemkxuLgqZt-Jzdeku_NRsAXj_7JmNx-6CIU1Ho34LP0fZom1x9cXl-AHWeXKSZA2	utilization-management.powerappsportals.com	/	2026-09-15T04:53:25.951Z	177	✓	✓	Lax			Medium
isDSTObserved	false	utilization-management.powerappsportals.com	/	Session	18		✓	Lax			Medium
isDSTSupport	false	utilization-management.powerappsportals.com	/	Session	17		✓	Lax			Medium
timeZoneCode	190	utilization-management.powerappsportals.com	/	Session	15	✓	✓	Lax			Medium
timezoneoffset	-330	utilization-management.powerappsportals.com	/	Session	18		✓	Lax			Medium"""

cookies = []
for line in cookie_text.strip().split('\n'):
    parts = line.split('\t')
    if len(parts) >= 4:
        name = parts[0]
        value = parts[1]
        domain = parts[2]
        path = parts[3]
        
        expires = -1
        # It seems the 5th column might be expiration or session. We'll set expires = -1 for session
        if parts[4] != 'Session':
            # rough estimate or just keep as session for now
            pass
            
        secure = '✓' in parts
        httpOnly = '✓' in parts # actually, parts 6 and 7 are usually httpOnly and Secure, but we can just set them
        
        sameSite = "Lax"
        if "None" in parts:
            sameSite = "None"
        elif "Strict" in parts:
            sameSite = "Strict"
            
        cookie = {
            "name": name,
            "value": value,
            "domain": domain,
            "path": path,
            "expires": -1,
            "httpOnly": True,
            "secure": True,
            "sameSite": sameSite
        }
        cookies.append(cookie)

user_json_path = r"c:\Users\Anant\Downloads\TOI-space\TOI-space-TOI_Project\configs\user.json"

with open(user_json_path, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Update existing cookies or append
existing_names = {c['name']: i for i, c in enumerate(data.get('cookies', []))}

for c in cookies:
    if c['name'] in existing_names:
        data['cookies'][existing_names[c['name']]] = c
    else:
        data['cookies'].append(c)

with open(user_json_path, 'w', encoding='utf-8') as f:
    json.dump(data, f, indent=2)

print("Cookies updated successfully.")
