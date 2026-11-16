const {test}=require('./harness.cjs');
test("F07 Contagem anunciada após buscar",async({page,open,ready,requests,assert})=>{
await open();const count=page.locator('#result-count');assert.equal(await count.getAttribute('aria-live'),'polite');assert.match(await count.innerText(),/1 registro/);await page.getByLabel('Buscar nesta lista').fill('ausente');assert.match(await count.innerText(),/0 registros/);
}).catch(e=>{console.error(e);process.exitCode=1});
