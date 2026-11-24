const express = require('express');
const crypto = require('crypto');
const fs = require('fs');
const app = express();
app.use(express.json());

const OWNER_NAME = "SujucDerBreite";
const OWNER_MASTER_SECRET = "SUPER_GEHEIMES_PASSWORT_NUR_FUER_SUJUCDERBREITE";
const DB_FILE = './licenses.json';

if (!fs.existsSync(DB_FILE)) fs.writeFileSync(DB_FILE, JSON.stringify({}));

// Nur Owner SujucDerBreite darf neue Lizenzen erstellen
app.post('/api/v1/create', (req, res) => {
    const { owner, secret, customer } = req.body;
    if (owner !== OWNER_NAME || secret !== OWNER_MASTER_SECRET) {
        return res.status(403).json({ error: "Zugriff verweigert. Nur SujucDerBreite kann Lizenzen erstellen." });
    }

    const newKey = "LOBBY-" + crypto.randomBytes(8).toString('hex').toUpperCase();
    const db = JSON.parse(fs.readFileSync(DB_FILE));
    db[newKey] = {
        customer: customer || "Unbekannt",
        createdBy: OWNER_NAME,
        createdAt: new Date().toISOString(),
        boundIp: null,
        active: true
    };
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
    res.json({ licenseKey: newKey, info: db[newKey] });
});

// Plugin-Verifizierung beim Serverstart
app.post('/api/v1/verify', (req, res) => {
    const { licenseKey, port } = req.body;
    const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
    const db = JSON.parse(fs.readFileSync(DB_FILE));

    const entry = db[licenseKey];
    if (!entry || !entry.active) {
        return res.status(401).json({ valid: false });
    }

    const identifier = `${clientIp}:${port}`;
    if (!entry.boundIp) {
        entry.boundIp = identifier;
        fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2));
    } else if (entry.boundIp !== identifier) {
        return res.status(403).json({ valid: false, reason: "Lizenz bereits an einen anderen Server gebunden." });
    }

    res.json({ valid: true });
});

app.listen(3000, () => console.log(`Lizenz-Server aktiv für Owner ${OWNER_NAME} auf Port 3000`));
