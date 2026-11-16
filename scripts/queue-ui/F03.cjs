const {test}=require('./harness.cjs');
test("F03 Situação canônica reflete URL normalizada",async({page,open,ready,requests,assert})=>{
await open('entregas?situacao=%20publicada%20');assert.equal(await page.getByLabel('Situação da avaliação',{exact:true}).inputValue(),'PUBLICADA');assert(page.url().includes('situacao=PUBLICADA'));assert(requests.some(q=>q==='situacao=PUBLICADA'));
}).catch(e=>{console.error(e);process.exitCode=1});
