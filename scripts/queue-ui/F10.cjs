const {test}=require('./harness.cjs');
test("F10 Aviso explica privacidade do aluno",async({page,open,ready,requests,assert})=>{
await open();const help=page.locator('#evaluation-help');assert.match(await help.innerText(),/Rascunhos e feedback não publicados são privados/);assert.equal(await page.getByLabel('Situação da avaliação',{exact:true}).getAttribute('aria-describedby'),'evaluation-help');
}, {role:"ALUNO"}).catch(e=>{console.error(e);process.exitCode=1});
