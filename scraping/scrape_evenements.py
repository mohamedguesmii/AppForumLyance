from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup
import time

def scrape_meetup_events():
    chrome_options = Options()
    chrome_options.add_argument("--headless")  # Enlève si tu veux voir le navigateur
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")

    driver = webdriver.Chrome(options=chrome_options)
    url = "https://www.meetup.com/fr-FR/find/events/"  # Page des événements publics Meetup
    driver.get(url)

    try:
        WebDriverWait(driver, 20).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, 'ul.list-reset'))
        )
    except Exception:
        print("[ERREUR] Timeout : événements non chargés.")
        driver.quit()
        return []

    time.sleep(5)  # Temps pour que JS charge bien

    soup = BeautifulSoup(driver.page_source, 'html.parser')
    driver.quit()

    events = []

    # Meetup liste ses événements dans une liste ul > li
    event_items = soup.select('ul.list-reset > li')
    for item in event_items:
        title_tag = item.select_one('h3')
        title = title_tag.text.strip() if title_tag else "Titre inconnu"

        # Lien vers la page de l'événement
        link_tag = item.select_one('a.eventCard--link')
        href = link_tag['href'] if link_tag and link_tag.has_attr('href') else "#"

        # Date et heure
        date_tag = item.select_one('time')
        date = date_tag.text.strip() if date_tag else "Date inconnue"

        # Lieu (si disponible)
        lieu_tag = item.select_one('.venueDisplay')
        lieu = lieu_tag.text.strip() if lieu_tag else "Lieu non précisé"

        # Description courte (optionnel, meetup ne donne pas toujours)
        description = "Voir plus sur Meetup"

        events.append({
            'title': title,
            'date': date,
            'lieu': lieu,
            'description': description,
            'url': href
        })

    print(f"[DEBUG] {len(events)} événements récupérés.")
    return events

if __name__ == "__main__":
    evenements = scrape_meetup_events()
    for i, e in enumerate(evenements, 1):
        print(f"{i}. {e['title']} - {e['date']} - {e['lieu']}")
