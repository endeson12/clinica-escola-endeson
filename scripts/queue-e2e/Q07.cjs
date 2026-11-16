const {test}=require('./harness.cjs');
test("Q07 Navegador restaura filtros ao voltar e recarregar",async({browser,base,tag,admin,teacher,student,p,st,turma,discipline,enrollment,activity,delivery,actor,person,create,get,queue,grade,ready,page,assert})=>{
const ui=await page();const select=ui.getByLabel('Situação da avaliação',{exact:true});await select.selectOption('PUBLICADA');await ui.waitForFunction(()=>location.hash.includes('situacao=PUBLICADA'));await ready(ui);assert.equal(await ui.locator('tbody tr').count(),0);
await select.selectOption('PENDENTE');await ui.waitForFunction(()=>location.hash.includes('situacao=PENDENTE'));await ready(ui);await ui.goBack();await ready(ui);await ui.waitForFunction(()=>document.querySelector('#evaluation-status')?.value==='PUBLICADA');assert.equal(await ui.locator('tbody tr').count(),0);
await ui.reload();await ready(ui);assert.equal(await select.inputValue(),'PUBLICADA');assert(ui.url().includes('atividade='+activity));
}).catch(e=>{console.error(e);process.exitCode=1});
