import requests
from datetime import datetime
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup
import time

BACKEND_URL = "http://localhost:8089/api/evenements/scraping/auto"

def envoyer_evenement_formdata(event):
    data = {
        'title': event.get('title', 'Titre par défaut'),
        'description': event.get('description', 'Description automatique'),
        'capacity': int(event.get('capacity', 100)),
        'datedebut': event.get('datedebut', datetime.today().strftime('%Y-%m-%d')),
        'datefin': event.get('datefin', datetime.today().strftime('%Y-%m-%d')),
        'adresse': event.get('adresse', 'Adresse inconnue'),
        'imageUrl': event.get('imageUrl', None),
        'source': 'eventbrite-forums',
    }
    try:
        response = requests.post(BACKEND_URL, json=data)
        if response.status_code == 201:
            print(f"✔ Événement ajouté : {data['title']}")
        elif response.status_code == 409:
            print(f"⚠ Événement déjà existant : {data['title']}")
            # Ajout forcé avec titre modifié
            test_title = data['title'] + " - Test " + datetime.now().strftime("%H:%M:%S")
            data['title'] = test_title
            response = requests.post(BACKEND_URL, json=data)
            if response.status_code == 201:
                print(f"➕ Ajout forcé avec nouveau titre : {test_title}")
            else:
                print(f"✘ Échec (même avec titre modifié) : {response.status_code}")
        else:
            print(f"✘ Échec {response.status_code} : {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Erreur lors de l'envoi : {e}")

def scrape_eventbrite_forums(pages=3):
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")

    driver = webdriver.Chrome(options=chrome_options)
    forums = []

    for page in range(1, pages + 1):
        url = f"https://www.eventbrite.fr/d/france--paris/forums--events/?page={page}"
        print(f"[INFO] Scraping page : {url}")
        driver.get(url)

        try:
            WebDriverWait(driver, 20).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, 'li > div'))
            )
        except Exception:
            print(f"⚠ La page {page} n’a pas chargé les événements.")
            continue

        time.sleep(3)
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        forum_divs = soup.select('li > div[class*="SearchResultPanelContentEventCardList-module"]')

        for div in forum_divs:
            # Tentatives multiples pour trouver le titre
            title_tag = (
                div.select_one('div[role="heading"]') or
                div.select_one('h3') or
                div.select_one('a') or
                div.select_one('span[class*="title"]')
            )
            title = title_tag.text.strip() if title_tag and title_tag.text.strip() else "Titre inconnu"
            print(f"DEBUG - Titre extrait : '{title}'")

            if title == "Titre inconnu" or not title:
                print("⚠ Titre vide ou inconnu, événement ignoré.")
                continue

            time_tag = div.select_one('time')
            date = time_tag['datetime'] if time_tag and time_tag.has_attr('datetime') else None
            if not date and time_tag:
                date = time_tag.text.strip()
            if date:
                try:
                    parsed_date = datetime.fromisoformat(date).strftime('%Y-%m-%d')
                except ValueError:
                    parsed_date = datetime.today().strftime('%Y-%m-%d')
            else:
                parsed_date = datetime.today().strftime('%Y-%m-%d')

            lieu_tag = div.select_one('div[class*="venue"]') or div.select_one('div[class*="card-text"]')
            lieu = lieu_tag.text.strip() if lieu_tag else "Lieu inconnu"

            link_tag = div.select_one('a')
            lien = link_tag['href'] if link_tag and link_tag.has_attr('href') else "N/A"

            img_tag = div.select_one('img')
            image_url = img_tag['src'] if img_tag and img_tag.has_attr('src') else None

            description = f"Forum - Date: {parsed_date}\nLieu: {lieu}\nPlus d'infos: {lien}"

            event = {
                "title": title,
                "description": description,
                "capacity": 100,
                "datedebut": parsed_date,
                "datefin": parsed_date,
                "adresse": lieu,
                "imageUrl": image_url
            }
            forums.append(event)

        print(f"[PAGE {page}] {len(forum_divs)} événements trouvés.")

    driver.quit()
    print(f"[TOTAL] {len(forums)} forums récupérés.")
    return forums

if __name__ == "__main__":
    forums = scrape_eventbrite_forums(pages=3)
    for event in forums:
        envoyer_evenement_formdata(event)
