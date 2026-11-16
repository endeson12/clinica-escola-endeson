const {test}=require('./harness.cjs');
test("F01 Busca ignora acentos",async({page,open,ready,requests,assert})=>{
await open(); await page.getByLabel('Buscar nesta lista').fill('avaliacao'); assert.equal(await page.locator('tbody tr').count(),1);
}).catch(e=>{console.error(e);process.exitCode=1});
