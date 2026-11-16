// Real Chromium + actual static assets. API fixtures are intentionally simulated.
const {chromium}=require('playwright');
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const root=path.resolve(__dirname,'../../src/main/resources/static');
const sample=(i=1)=>({id:i,atividade_titulo:'Avaliação de atenção '+i,turma_nome:'Turma QA',aluno_nome:'João QA '+i,enviado_em:'2026-11-01T12:00:00Z',publicada:false,nota:null,conteudo:'Resposta '+i});
async function test(name,fn,options={}) {
 const browser=await chromium.launch({executablePath:process.env.CHROME_PATH||'/usr/bin/google-chrome',args:['--no-sandbox']});
 const page=await browser.newPage({viewport:options.viewport||{width:1280,height:900}});
 page.setDefaultTimeout(5000);
 const requests=[],errors=[];
 let fail=options.fail||false;
 page.on('pageerror',e=>errors.push(e.message));
 await page.route('http://portal.test/**',async route=>{
  const u=new URL(route.request().url());
  if(u.pathname.startsWith('/api/')) {
   let body=[];
   if(u.pathname.endsWith('/contexto'))body={usuario:{id:1,nome:'QA LEM',perfil:options.role||'PROFESSOR'},unidade:{id:1,nome:'QA isolado'}};
   if(u.pathname.endsWith('/entregas')) {
    requests.push(u.searchParams.toString());
    if(fail)return route.fulfill({status:503,json:{message:'Falha QA controlada'}});
    body=options.rows||[sample()];
    if(options.emptyPublished&&u.searchParams.get('situacao')==='PUBLICADA')body=[];
   }
   return route.fulfill({json:body});
  }
  const f=path.join(root,u.pathname);
  if(!f.startsWith(root+path.sep)||!fs.existsSync(f))return route.fulfill({status:404,body:''});
  return route.fulfill({body:fs.readFileSync(f),contentType:f.endsWith('.js')?'application/javascript':f.endsWith('.css')?'text/css':'text/html'});
 });
 const ready=()=>page.waitForFunction(()=>document.querySelector('#conteudo')?.getAttribute('aria-busy')==='false');
 const open=async(hash='entregas')=>{await page.goto('http://portal.test/portal.html#'+hash);await ready();};
 try {await fn({page,open,ready,requests,setFailure:v=>{fail=v},assert,sample});assert.deepEqual(errors,[]);console.log('PASS '+name);}
 finally {await browser.close();}
}
module.exports={test,sample};
